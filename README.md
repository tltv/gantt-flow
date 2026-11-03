# Gantt Flow addon

Project root of the Gantt component addon, Flow integration for Vaadin 25 platform.

Multi-module Maven project with two modules: gantt-flow-addon and gantt-flow-demo. 

- `gantt-flow-addon` contains add-on sources build into JAR deliverable.
- `gantt-flow-demo` contains demo application build into WAR package.

# Example

```java
Gantt gantt = new Gantt();
Step step = new Step();
step.setCaption("First Step");
step.setBackgroundColor("#9cfb84");
step.setStartDate(LocalDate.now().atTime(12, 0));
step.setEndDate(step.getStartDate().plusWeeks(1));
gantt.addStep(step);

```

# Dependencies

Main component class `org.vaadin.tltv.gantt.Gantt` is a Java wrapper for `tltv-gantt-element` web component. This requires two dependecies:
- `tltv-gantt-element` (https://github.com/tltv/gantt-element)
- `tltv-timeline-element` (https://github.com/tltv/timeline-element).

Timeline element and gantt element depends on following libraries:
- Lit (https://lit.dev/)
- date-fns (https://github.com/date-fns/date-fns) 
- date-fns-tz (https://github.com/marnusw/date-fns-tz)

# Install

Latest version and instructions are availabe in Vaadin Directory: https://vaadin.com/directory/component/gantt-flow-add-on

## Development instructions

Building the component and demo in development mode:
1. Run `mvn clean install`

Starting the gantt-flow-demo server in development mode:
1. `cd gantt-flow-demo`
2. Run `mvn jetty:run`
3. Open http://localhost:8080 in the browser.

Building the component and demo in production mode and running it:
1. Run `mvn package`
2. `cd gantt-flow-demo`
3. Run `mvn jetty:run-war`

Building deliverable for Vaadin Directory:
1. `cd gantt-flow-addon`
2. Run `mvn clean install -Pdirectory`

## License
MIT - Tomi Virtanen
