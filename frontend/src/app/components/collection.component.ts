import { UsersService } from '../services/users.service';
import { GamesService } from '../services/games.service';
import { CollectionService } from '../services/collection.service';
import { Component, OnInit } from '@angular/core';
import { User } from '../data/user';
import { Game } from '../data/game';
import { AlertService } from '../services/alert.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
    templateUrl: '../views/collection.component.html',
    styleUrls: ['../styles/collection.component.css']
})
export class CollectionComponent implements OnInit {
    users: User[];
    shownGames: Game[];
    selectedUser: User;
    totalItems: number;
    currentPage: number = 0;
    focusedGame: Game;
    searchTerm: string;
    searchResult: Game[];

    constructor(private usersService: UsersService, 
        private gamesService: GamesService, 
        private alertService: AlertService,
        private collectionService: CollectionService) {}

    ngOnInit() {
        this.usersService.getAllUsers().subscribe(data => {
            this.users = data;
            this.users.forEach(user => {
                this.collectionService.getTotalGamesForUser(user.bggNick).subscribe(data => user.collectionSize = data.totalGames);
            });
        });
    }

    selectUser(user: User) {
        this.selectedUser = user;
        this.collectionService.getPagesForUser(user.bggNick).subscribe(data => this.totalItems = data.pageSize * data.totalPages);
        this.collectionService.getPageForUser(user.bggNick, 0).subscribe(data => this.shownGames = data);
        this.searchResult = null;
    }

    pageChange(newPage: number) {
        this.collectionService.getPageForUser(this.selectedUser.bggNick, newPage - 1).subscribe(data => this.shownGames = data);
        this.currentPage = newPage;
    }

    removeGame(game: Game) {
        this.collectionService.removeGameForUser(this.selectedUser.bggNick, game.id).subscribe(
            data => {
                this.alertService.success('Removed game ' + data.game + ' for user ' + data.user);
                this.collectionService.getPageForUser(this.selectedUser.bggNick, this.currentPage - 1).subscribe(data => this.shownGames = data);
                this.collectionService.getTotalGamesForUser(this.selectedUser.bggNick).subscribe(data => this.selectedUser.collectionSize = data.totalGames);
            },
            (err: HttpErrorResponse) => {
                if (err.status == 200) {
                    this.alertService.success('Removed game ' + game.id + ' for user ' + this.selectedUser.bggNick);
                    this.collectionService.getPageForUser(this.selectedUser.bggNick, this.currentPage).subscribe(data => this.shownGames = data);
                    this.collectionService.getTotalGamesForUser(this.selectedUser.bggNick).subscribe(data => this.selectedUser.collectionSize = data.totalGames);
                } else {
                    this.alertService.error('Can\'t remove game (' + err.status + ')');
                }
            }
        );
    }

    startSearch() {
        this.collectionService.searchGame(this.searchTerm).subscribe(data => this.searchResult = data);
    }

    addGame(game: Game) {
        this.collectionService.addGameForUser(this.selectedUser.bggNick, game.id).subscribe(
            data => {
                this.alertService.success('added game for user');
                this.collectionService.getTotalGamesForUser(this.selectedUser.bggNick).subscribe(data => this.selectedUser.collectionSize = data.totalGames);
                this.collectionService.getPageForUser(this.selectedUser.bggNick, this.currentPage).subscribe(data => this.shownGames = data);
            }
        )
    }
}