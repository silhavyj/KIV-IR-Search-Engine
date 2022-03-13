module cz.zcu.kiv.ir.silhavyj.searchengine {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.apache.opennlp.tools;
    requires org.json;
    requires lingua;
    requires org.jsoup;

    opens cz.zcu.kiv.ir.silhavyj.searchengine to javafx.fxml;

    exports cz.zcu.kiv.ir.silhavyj.searchengine;
    exports cz.zcu.kiv.ir.silhavyj.searchengine.gui;
    opens cz.zcu.kiv.ir.silhavyj.searchengine.gui to javafx.fxml;
}