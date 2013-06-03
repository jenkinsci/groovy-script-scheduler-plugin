/*
 * The MIT License
 *
 * Copyright (c) 2013 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.groovyscriptscheduler;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import hudson.Extension;

import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.jenkinsci.plugins.externalscheduler.NodeAssignments;
import org.jenkinsci.plugins.externalscheduler.Scheduler;
import org.kohsuke.stapler.DataBoundConstructor;

public class GroovyScriptScheduler extends Scheduler {

    private final static Logger LOGGER = Logger.getLogger(GroovyScriptScheduler.class.getName());

    private final String script;

    @DataBoundConstructor
    public GroovyScriptScheduler(final String script) {

        this.script = script;
    }

    public String getScript() {

        return script;
    }

    @Override
    public NodeAssignments solution() {

        final CompilerConfiguration config = new CompilerConfiguration().addCompilationCustomizers(
                new ImportCustomizer().addStarImports(
                        "jenkins",
                        "jenkins.model",
                        "hudson",
                        "hudson.model",
                        "org.jenkinsci.plugins.externalscheduler"
                )
        );

        final Binding binding = new Binding();
        binding.setVariable("stateProvider", stateProvider());

        final ClassLoader classLoader = Jenkins.getInstance().getPluginManager().uberClassLoader;
        final GroovyShell shell = new GroovyShell(classLoader, binding, config);
        try {

            final Object solution = shell.evaluate(script);

            if (solution == null || solution instanceof NodeAssignments) {

                return (NodeAssignments) solution;
            }

            final String message = "An instance of " + solution.getClass().getName() + " returned from Groovy Script";
            LOGGER.log(Level.SEVERE, message, solution);

        } catch (Throwable ex) {

            LOGGER.log(Level.SEVERE, "Exception thrown from Groovy Script", ex);
        }

        return null;
    }

    @Extension
    public static class Descriptor extends Scheduler.Descriptor {

        @Override
        public String getDisplayName() {

            return "Use custom Groovy Script";
        }
    }
}
