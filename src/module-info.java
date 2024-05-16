module ChaCuN {
    requires javafx.controls;
    requires java.net.http;
    requires java.desktop;
    requires jdk.compiler;
    requires transitive javafx.media;

    exports ch.epfl.chacun;
    exports ch.epfl.chacun.gui;
}
