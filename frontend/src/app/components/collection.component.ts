import { UsersService } from '../services/users.service';
import { GamesService } from '../services/games.service';
import { CollectionService } from '../services/collection.service';
import { Component, OnInit } from '@angular/core';
import { User } from '../data/user';
import { Game } from '../data/game';

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

    constructor(private usersService: UsersService, 
        private gamesService: GamesService, 
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
    }

    pageChange(newPage: number) {
        this.collectionService.getPageForUser(this.selectedUser.bggNick, newPage - 1).subscribe(data => this.shownGames = data);
        this.currentPage = newPage;
    }
}