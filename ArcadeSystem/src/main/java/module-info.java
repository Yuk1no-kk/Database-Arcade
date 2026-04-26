module com.example.arcadesystem {
    requires javafx.controls;
    requires javafx.fxml;

    // ==================== 新增的核心修复代码 ====================
    // 1. 声明依赖java.sql模块，直接解决「java.sql包不可见」的报错
    requires java.sql;

    // 2. 开放当前包给JavaFX和MySQL驱动（反射需要，后续连数据库必加）
    opens com.example.arcadesystem to javafx.fxml, mysql.connector.java;
    // 导出当前包，供外部模块访问
    exports com.example.arcadesystem;

}