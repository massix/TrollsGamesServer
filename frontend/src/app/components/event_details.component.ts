import { Component, OnInit, Input } from '@angular/core';
import { Event } from '../data/event';
import { TablesService } from '../services/tables.service';
import { Table } from '../data/table';
import { EventsService } from '../services/events.service';

@Component({
    selector: 'event-details',
    template:
    `
    <div class="container">
        <h4 style="margin-bottom: 20px">{{event.name}}</h4>

        <!-- Tables -->
        <div class="row">
            <ul class="col-md-4 list-group">
                <li *ngFor="let table of tables" class="list-group-item">
                    {{table.name}} ({{table.minPlayers}} - {{table.maxPlayers}} players)
                    <button class="btn btn-sm fa fa-remove pull-right" (click)="removeTable(table)"></button>
                    <button class="btn btn-sm fa fa-search pull-right" (click)="getDetails(table)"></button>
                </li>
            </ul>

            <div class="col-md-3">
                Table details.
            </div>
        </div>

        <!-- Add a table -->
        <div class="row">
            <div class="col-md-12 event-add-table form-group">
                <form class="form form-compact form-inline">
                    <button class="btn btn-sm fa fa-plus" (click)="addTableToEvent(selectedTable.value)"></button>
                    <select #selectedTable class="form-control" id="addtable">
                        <option *ngFor="let table of allTables" [value]="table.id">{{table.name}}</option>
                    </select>
                </form>
            </div>
        </div>
    </div>
    `
})
export class EventDetailsComponent implements OnInit {
    private _event: Event;
    tables: Table[];

    allTables: Table[];

    constructor(private tablesService: TablesService, private eventsService: EventsService) {}

    refresh() {
        this.eventsService.getTablesForEvent(this._event.id).subscribe(data => {
            this.tables = data;
            this._event.tables = data.length;
        });
        this.tablesService.getTables().subscribe(data => this.allTables = data);
    }

    ngOnInit() {
        this.refresh();
    }

    @Input()
    set event(event: Event) {
        this._event = event;
        this.refresh();
    }

    get event() {
        return this._event;
    }

    addTableToEvent(table: number) {
        this.eventsService.addTableForEvent(this._event.id, table).subscribe(
            data => {
                this.refresh();
            }
        );
    }

    removeTable(table: Table) {
        this.eventsService.removeTableForEvent(this._event.id, table.id).subscribe(
            data => this.refresh()
        );
    }
}