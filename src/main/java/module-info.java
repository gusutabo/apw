module com.gusutabo.apw {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    opens com.gusutabo.apw to javafx.fxml;
    exports com.gusutabo.apw;
}
