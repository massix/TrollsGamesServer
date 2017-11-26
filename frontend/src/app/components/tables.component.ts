import { Component, OnInit } from '@angular/core';
import { TablesService } from '../services/tables.service';
import { Table } from '../data/table';

@Component({
    templateUrl: '../views/tables.component.html'
})
export class TablesComponent implements OnInit {
    tables: Table[];
    newTable = new Table();

    constructor(private tablesService: TablesService) {}

    refreshAllTables() {
        this.tablesService.getTables().subscribe(data => this.tables = data);
        this.newTable = new Table();
    }

    saveTable(table: Table) {
        this.tablesService.createTable(table).subscribe(data => this.refreshAllTables());
    }

    removeTable(table: Table) {
        this.tablesService.removeTable(table).subscribe(data => this.refreshAllTables());
    }

    ngOnInit() {
        this.refreshAllTables();
    }
}