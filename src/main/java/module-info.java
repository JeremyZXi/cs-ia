module com.example.planner {
    requires javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires flexmark.util.ast;
    requires flexmark;
    requires flexmark.util.data;
    requires flexmark.util.builder;
    requires javafx.media;
    requires opencsv;
    requires se.alipsa.ymp;


    // --- Open to JavaFX ---
    opens com.example.planner to javafx.fxml;
    opens com.example.planner.module to com.fasterxml.jackson.databind;
    opens com.example.planner.utility to com.fasterxml.jackson.databind;

    // --- Exported packages ---
    exports com.example.planner;
    exports com.example.planner.module;
    exports com.example.planner.utility;
}
