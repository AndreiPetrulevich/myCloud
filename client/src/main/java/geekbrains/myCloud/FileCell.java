package geekbrains.myCloud;

import javafx.beans.binding.Bindings;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.StringConverter;

import java.util.Optional;
import java.util.function.Consumer;

public class FileCell extends TextFieldListCell<String> {
    public class FilenamePair {
        String oldName;
        String newName;

        public FilenamePair(String oldName, String newName) {
            this.oldName = oldName;
            this.newName = newName;
        }
    }

    private Consumer<FilenamePair> onRenamed;
    private Consumer<String> onDelete;

    public FileCell(Consumer<FilenamePair> onRenamed, Consumer<String> onDelete) {
        super(new StringConverter<String>() {
            @Override
            public String toString(String s) {
                return Optional.ofNullable(s).orElse("");
            }

            @Override
            public String fromString(String s) {
                return Optional.ofNullable(s).orElse("");
            }
        });
        this.onRenamed = onRenamed;
        this.onDelete = onDelete;

        ContextMenu contextMenu = new ContextMenu();
        this.setEditable(false);

        MenuItem renameFile = new MenuItem();
        renameFile.textProperty().bind(Bindings.format("Rename", this.itemProperty()));
        renameFile.setOnAction(event -> {
            this.setEditable(true);
            this.startEdit();
        });

        MenuItem deleteFile = new MenuItem();
        deleteFile.textProperty().bind(Bindings.format("Delete", this.itemProperty()));
        deleteFile.setOnAction(event -> {
            this.onDelete.accept(this.getItem());
        });

        contextMenu.getItems().addAll(renameFile, deleteFile);

        this.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
            if(isNowEmpty) {
                this.setContextMenu(null);
            } else {
                this.setContextMenu(contextMenu);
            }
        });
    }

    @Override
    public void updateItem(String item, boolean empty) {
        String old = this.getItem();
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item);
            if (old != null && item != null && old != item) {
                onRenamed.accept(new FilenamePair(old, item));
                this.setEditable(false);
            }
        }
    }
}
