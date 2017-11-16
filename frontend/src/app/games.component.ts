import { Component, OnInit } from '@angular/core';
import { GamesService } from './games.service';
import { Game } from './game';
import { HttpErrorResponse } from '@angular/common/http';
import { AlertService } from './alert.service';

@Component({
    templateUrl: './games.component.html',
    styleUrls: ['./games.component.css']
})
export class GamesComponent implements OnInit {
    games: Game[];
    currentPage: number = 0;
    totalItems: number;
    selectedGame: Game;

    pageChange(newPage: number) {
        this.selectedGame = null;
        this.gamesService.getPageNumber(newPage - 1).subscribe(data => this.games = data);
        this.currentPage = newPage;
    }

    ngOnInit() {
        this.gamesService.getTotalPages().subscribe(data => {
            this.totalItems = data.totalPages * data.pageSize;
        });

        this.gamesService.getPageNumber(0).subscribe(data => this.games = data);
    }

    showHideGame(game: Game) {
        if (this.selectedGame === game) {
            this.selectedGame = null;
        } else {
            this.selectedGame = game;
        }
    }

    removeGame(game: Game) {
        this.gamesService.removeGame(game.id).subscribe(
            data => {
                const page = this.currentPage;
                this.alertService.success('Game removed');
                this.pageChange(this.currentPage + 1);
                this.currentPage = page;
                this.totalItems--;
            },
            (error: HttpErrorResponse) => {
                this.alertService.error('Code ' + error.status);
            }
        );

        if (this.selectedGame === game) {
            this.selectedGame = null;
        }
    }

    constructor(private gamesService: GamesService, private alertService: AlertService) {}
}
