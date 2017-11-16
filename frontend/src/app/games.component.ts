import { Component, OnInit } from '@angular/core';

@Component({
    templateUrl: './games.component.html',
    styleUrls: ['./games.component.css']
})
export class GamesComponent implements OnInit {
    ngOnInit() {
        console.log('up and running');
    }

    constructor() {
        console.log('games component');
    }
}
