import { Component, OnInit } from '@angular/core';
import { GamesService } from '../services/games.service';
import { Game } from '../data/game';
import { HttpErrorResponse } from '@angular/common/http';
import { AlertService } from '../services/alert.service';
import { Subject } from 'rxjs/Subject';

@Component({
    templateUrl: '../views/games.component.html',
    styleUrls: ['../styles/games.component.css']
})
export class GamesComponent implements OnInit {
    games: Game[];
    currentPage: number = 0;
    totalItems: number;
    selectedGame: Game;
    searchTerm: string;
    inputTimer: any;

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

    searchTermChange() {
        clearTimeout(this.inputTimer);
        this.inputTimer = setTimeout(data => {
            if (this.searchTerm == '') {
                console.log('resetting');
                this.ngOnInit();
                this.currentPage = 0;
            } else {
                console.log('search for ' + this.searchTerm);
                this.gamesService.searchGame(this.searchTerm).subscribe(data => {
                    this.games = data;
                    this.totalItems = data.length;
                    this.currentPage = 0;
                });
            }
        }, 2000);
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

    recrawlGame(game: Game) {
        game.name = 'recrawling';
        game.description = 'recrawling';
        this.gamesService.recrawlGame(game.id).subscribe(
            data => {
                this.gamesService.getGame(game.id).subscribe(data => {
                    game.name = data.name;
                    game.description = data.description;
                });
            },
            (error: HttpErrorResponse) => {
                this.alertService.error('Code ' + error.status + ' - ' + error.error);
            }
        )
    }

    constructor(private gamesService: GamesService, private alertService: AlertService) {}
}
