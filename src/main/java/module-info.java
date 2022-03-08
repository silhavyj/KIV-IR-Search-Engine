module cz.zcu.kiv.ir.silhavyj.searchengine {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens cz.zcu.kiv.ir.silhavyj.searchengine to javafx.fxml;
    exports cz.zcu.kiv.ir.silhavyj.searchengine;
}