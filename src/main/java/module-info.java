module com.gusutabo.apw {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.gusutabo.apw to javafx.fxml;
    exports com.gusutabo.apw;
}
