import { Component, OnInit } from '@angular/core';
import { Event } from '../data/event';
import { EventsService } from '../services/events.service';
import { Table } from '../data/table';
import { TablesService } from '../services/tables.service';

@Component({
    templateUrl: '../views/rdv.component.html',
    styleUrls: ['../styles/rdv.component.css']
})
export class RdvComponent implements OnInit {
    events: Event[];
    tables: Table[];
    selectedEvent: Event;

    constructor(private eventsService: EventsService, private tablesService: TablesService) {}

    refreshAllEvents() {
        this.eventsService.getAllEvents().subscribe(data => {
            this.events = data;
            this.events.forEach(event => {
                this.eventsService.getTablesForEvent(event.id).subscribe(data => {
                    event.tables = data.length;
                })
            })
        });

        this.tablesService.getTables().subscribe(data => {
            this.tables = data;
        });

        this.selectedEvent = null;
    }

    ngOnInit() {
        this.refreshAllEvents();
    }
}