/*
 * Copyright 2015 deltedes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.phante.sarabandasaloon.ui;

import com.phante.sarabandasaloon.entity.Song;
import com.phante.sarabandasaloon.entity.TrackList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;

/**
 *
 * @author deltedes
 */
public class SongTableView extends TableView<Song> {

    private TableColumn<Song, String> idColumn = new TableColumn<>();
    private TableColumn<Song, String> titleColumn = new TableColumn<>();
    private TableColumn<Song, String> artistColumn = new TableColumn<>();
    private TableColumn<Song, String> albumColumn = new TableColumn<>();
    private TableColumn<Song, Boolean> playedColumn = new TableColumn<>();
    private TableColumn<Song, Boolean> OKColumn = new TableColumn<>();
    private TableColumn<Song, Boolean> KOColumn = new TableColumn<>();
    
    private FilteredList<Song> filteredData;
    private SortedList<Song> sortedData;

    public SongTableView() {
        super();
        Logger.getLogger(SongTableView.class.getName()).log(Level.INFO, "Inizializzo la SongTableView");

        this.getColumns().clear();

        // Imposta le singole colonne
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        this.getColumns().add(idColumn);

        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        this.getColumns().add(titleColumn);

        albumColumn.setCellValueFactory(cellData -> cellData.getValue().albumProperty());
        this.getColumns().add(albumColumn);

        artistColumn.setCellValueFactory(cellData -> cellData.getValue().artistProperty());
        this.getColumns().add(artistColumn);

        playedColumn.setCellValueFactory(cellData -> cellData.getValue().playedProperty());
        playedColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());
        this.getColumns().add(playedColumn);

        OKColumn.setCellValueFactory(cellData -> cellData.getValue().okProperty());
        OKColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());
        this.getColumns().add(OKColumn);

        KOColumn.setCellValueFactory(cellData -> cellData.getValue().koProperty());
        KOColumn.setCellFactory((TableColumn<Song, Boolean> p) -> new CheckBoxTableCell<>());
        this.getColumns().add(KOColumn);
    }
    
    public void setItem(ObservableList<Song> list) {
        // Crea le versioni filtrate e ordinate della tabella
        filteredData = new FilteredList<>(list, p -> true);
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(this.comparatorProperty());
        
        this.setItems(sortedData);
    }
    
    public ChangeListener<String> getFilterListener() {
        return ((observable, oldValue, newValue) -> {
            filteredData.setPredicate(song -> {
                // Se il filtro è vuto mostra tutte le canzoni
                if (newValue == null || newValue.isEmpty()) {
                    return true;  
                }

                String lowerCaseFilter = newValue.toLowerCase();
                boolean found = false;
                // Verifica che l'id contenga il testo
                found = found || song.getId().toLowerCase().contains(lowerCaseFilter);
                // Verifica che il titolo contenga il testo
                found = found || song.titleProperty().getValue().toLowerCase().contains(lowerCaseFilter);
                return found;
            });
        });

    }    

}
