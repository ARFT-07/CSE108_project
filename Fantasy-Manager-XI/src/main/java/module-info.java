module org.buet.fantasymanagerxi {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens org.buet.fantasymanagerxi to javafx.fxml;
    exports org.buet.fantasymanagerxi;
    exports org.buet.fantasymanagerxi.model;
    opens org.buet.fantasymanagerxi.model to javafx.fxml;
}