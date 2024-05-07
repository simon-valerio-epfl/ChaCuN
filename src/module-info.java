module ChaCuN {
    requires javafx.controls;
    requires java.net.http;
    requires java.desktop;
    requires jdk.compiler;
    requires org.junit.jupiter.api;

    exports ch.epfl.chacun;
    exports ch.epfl.chacun.gui;
}
