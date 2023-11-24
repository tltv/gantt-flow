# Gantt Flow addon

Project root of the Gantt component addon, Flow integration for Vaadin 24 platform.

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
step.setEndDate(step.getSta.plusWeeks(1));
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

# Known issues
`formatInTimeZone` is not working in all cases (date-fns-tz 2.0.0). 
E.g. Gantt with "Europe/Berlin" time-zone shows wrong hour when run in Browser with "Europe/Helsinki" timezone.

# Install

Latest version and instructions are availabe in Vaadin Directory: https://vaadin.com/directory/component/gantt-flow-add-on

## Development instructions

Building the component and demo in development mode:
1. Run `mvn clean install`

Building the component and demo in production mode:
1. Run `mvn clean install -Pproduction`

Starting the gantt-flow-demo server:
1. `cd gantt-flow-demo`
2. Run `mvn jetty:run`
3. Open http://localhost:8080 in the browser.

Building deliverable for Vaadin Directory:
1. `cd gantt-flow-addon`
2. Run `mvn clean install -Pdirectory`

## License
MIT - Tomi Virtanen
