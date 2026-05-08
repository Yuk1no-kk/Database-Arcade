package com.example.arcadesystem;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HelloController {
    @FXML
    private Label welcomeText;
    @FXML
    private VBox memberButtons; // 功能按钮组容器
    @FXML
    private Label functionTip;  // 功能提示标签

    // 点击Hello按钮：显示功能按钮组
    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to Arcade System!");
        memberButtons.setVisible(true); // 显示4个会员操作按钮
        functionTip.setVisible(false); // 清空之前的提示
    }

    // 新增会员按钮点击事件
    @FXML
    protected void onAddMemberClick() {
        functionTip.setText("add member 功能制作中...");
        functionTip.setVisible(true);
    }

    // 删除会员按钮点击事件
    @FXML
    protected void onDeleteMemberClick() {
        functionTip.setText("delete member 功能制作中...");
        functionTip.setVisible(true);
    }

    // 编辑会员按钮点击事件
    @FXML
    protected void onEditMemberClick() {
        functionTip.setText("edit member 功能制作中...");
        functionTip.setVisible(true);
    }

    // 搜索会员按钮点击事件
    @FXML
    protected void onSearchMemberClick() {
        functionTip.setText("search member 功能制作中...");
        functionTip.setVisible(true);
    }
}