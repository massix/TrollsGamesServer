export class Event {
    id: number;
    name: string;
    start: number;
    end: number;

    // Number of tables associated to the event
    tables: number = 0;
}