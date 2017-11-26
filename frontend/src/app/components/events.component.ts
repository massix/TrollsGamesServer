import { Component, OnInit } from '@angular/core';
import { Event } from '../data/event';
import { EventsService } from '../services/events.service';

@Component({
    templateUrl: '../views/events.component.html'
})
export class EventsComponent implements OnInit {
    events: Event[];
    newEvent = new Event();

    constructor(private eventsService: EventsService) {}
    refreshAllEvents() {
        this.eventsService.getAllEvents().subscribe(data => this.events = data);
        this.newEvent = new Event();
    }

    ngOnInit() {
        this.refreshAllEvents();
    }

    saveEvent(event: Event) {
        console.log('saving event');
        console.log(event);
        this.eventsService.storeEvent(event).subscribe(
            data => {
                console.log('stored event');
                console.log(data);
                this.refreshAllEvents();
            },
            error => {
                console.log('error');
                console.log(error);
            }
        );
    }

    removeEvent(event: Event) {
        this.eventsService.removeEvent(event.id).subscribe(data => this.refreshAllEvents());
    }
}