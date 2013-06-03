# Groovy Script Scheduler Plugin

## Examples

Assign all items to `slave42`

    builder = NodeAssignments.builder();
    stateProvider.getQueue().each() { item ->
      builder.assign(item, Jenkins.instance.getNode('slave42'));
    }

    return builder.build();
