import { Component, OnInit } from '@angular/core';
import { GamesService } from './games.service';
import { Game } from './game';
import { HttpErrorResponse } from '@angular/common/http/src/response';
import { AlertService } from './alert.service';

@Component({
    templateUrl: './games.component.html',
    styleUrls: ['./games.component.css']
})
export class GamesComponent implements OnInit {
    games: Game[];
    currentPage: number = 0;
    totalItems: number;

    pageChange(newPage: number) {
        this.gamesService.getPageNumber(newPage - 1).subscribe(data => this.games = data);
        this.currentPage = newPage;
    }

    ngOnInit() {
        this.gamesService.getTotalPages().subscribe(data => {
            this.totalItems = data.totalPages * data.pageSize;
        });

        this.gamesService.getPageNumber(0).subscribe(data => this.games = data);
    }

    constructor(private gamesService: GamesService, private alertService: AlertService) {}
}
