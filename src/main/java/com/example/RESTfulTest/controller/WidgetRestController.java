package com.example.RESTfulTest.controller;

import com.example.RESTfulTest.model.Widget;
import com.example.RESTfulTest.service.WidgetService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@RestController
public class WidgetRestController {
    private static final Logger logger = LogManager.getLogger(WidgetRestController.class);

    @Autowired
    private WidgetService widgetService;

    @GetMapping("/rest/widget/{id}")
    public ResponseEntity<?> getWidget(@PathVariable Long id) {
        return widgetService.findById(id)
                .map(widget -> {
                    try {
                        return ResponseEntity
                                .ok()
                                .eTag(Integer.toString(widget.getVersion()))
                                .location(new URI("/rest/widget/" + widget.getId()))
                                .body(widget);
                    } catch (URISyntaxException e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rest/widgets")
    public ResponseEntity<List<Widget>> getWidgets() {
        try {
            return ResponseEntity.ok()
                    .location((new URI("/rest/widgets")))
                    .body(widgetService.findAll());
        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/rest/widget")
    public ResponseEntity<Widget> createWidget(@RequestBody Widget widget) {
        logger.info("Received widget: name: " + widget.getName() + ", description: " + widget.getDescription());
        Widget newWidget = widgetService.save(widget);

        try {
            return ResponseEntity
                    .created(new URI("/rest/widget/" + newWidget.getId()))
                    .eTag(Integer.toString(newWidget.getVersion()))
                    .body(newWidget);
        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/rest/widget/{id}")
    public ResponseEntity<Widget> updateWidget(@RequestBody Widget widget, @PathVariable Long id, @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        // Get the widget with the specified id
        Optional<Widget> existingWidget = widgetService.findById(id);
        if (!existingWidget.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        // Validate that the if-match header matches the widget's version
        if (!ifMatch.equalsIgnoreCase(Integer.toString(existingWidget.get().getVersion()))) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Update the widget
        widget.setId(id);
        widget = widgetService.save(widget);

        try {
            // Return a 200 response with the updated widget
            return ResponseEntity
                    .ok()
                    .eTag(Integer.toString(widget.getVersion()))
                    .location(new URI("/rest/widget/" + widget.getId()))
                    .body(widget);
        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/rest/proper/widget/{id}")
    public ResponseEntity<Widget> updateWidgetProper(@RequestBody Widget widget, @PathVariable Long id, @RequestHeader("If-Match") Integer ifMatch) {
        Optional<Widget> existingWidget = widgetService.findById(id);
        if (existingWidget.isPresent()) {
            if (ifMatch.equals(existingWidget.get().getVersion())) {
                widget.setId(id);
                return ResponseEntity.ok().body(widgetService.save(widget));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/rest/widget/{id}")
    public ResponseEntity deleteWidget(@PathVariable Long id) {
        widgetService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
