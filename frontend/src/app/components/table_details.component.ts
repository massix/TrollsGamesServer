import { Component, OnInit, Input } from '@angular/core';
import { Table } from '../data/table';
import { TablesService } from '../services/tables.service';
import { UsersService } from '../services/users.service';
import { GamesService } from '../services/games.service';
import { User } from '../data/user';
import { Game } from '../data/game';

@Component({
    selector: 'table-details',
    template:
    `
    <div class="col-md-12">
        <h4 style="margin-bottom: 20px">{{table.name}}</h4>
        <ul class="list-group">
            <li *ngFor="let game of games" class="list-group-item">
                <span class="fa fa-gamepad"></span> {{game.name}} ({{game.minPlayers}} - {{game.maxPlayers}} players)
                <button class="btn btn-sm fa fa-remove pull-right" (click)="removeGameFromTable(game)"></button>
            </li>
        </ul>
        <form class="form-group form-inline">
            <button class="btn btn-sm fa fa-plus" (click)="addGameToTable()"></button>
            <input autocomplete
                   [config]="{'placeholder' : 'search for game', 'sourceField': ['name']}"
                   [items]="searchResult" 
                   (inputChangedEvent)="searchGame($event)" 
                   (selectEvent)="selectGame($event)"
                   type="text" 
                   placeholder="game to add" />
        </form>
        <ul class="list-group">
            <li *ngFor="let user of users" class="list-group-item">
                <span class="fa fa-user"></span> 
                {{user.bggNick}}
                <button class="btn btn-sm fa fa-remove pull-right" (click)="removeUserFromTable(user)"></button>
            </li>
        </ul>
        <form class="form-group form-inline">
            <button class="btn btn-sm fa fa-plus" (click)="addUserToTable(selectedUser.value)"></button>
            <select #selectedUser class="form-control">
                <option *ngFor="let user of allUsers" [value]="user.bggNick">{{user.bggNick}} - {{user.forumNick}}</option>
            </select>
        </form>
    </div>
    `
})
export class TableDetailsComponent implements OnInit {
    private _table: Table;
    users: User[];
    games: Game[];
    searchResult: Game[] = [];
    searchTimer: any;

    gameToAdd: Game;

    allUsers: User[];

    @Input() set table(table: Table) {
        this._table = table;
        this.refresh();
    }

    get table(): Table {
        return this._table;
    }

    constructor(
        private tablesService: TablesService, 
        private usersService: UsersService, 
        private gamesService: GamesService) 
        {}

    refresh() {
        this.tablesService.getTable(this._table.id).subscribe(data => this._table = data);
        this.tablesService.getUsersForTable(this._table).subscribe(data => this.users = data);
        this.tablesService.getGamesForTable(this._table).subscribe(data => this.games = data);

        this.usersService.getAllUsers().subscribe(data => this.allUsers = data);
    }

    ngOnInit() {
        this.refresh();
    }

    addUserToTable(user: string) {
        this.tablesService.addUserToTable(this._table, user).subscribe(data => this.refresh());
    }

    removeUserFromTable(user: User) {
        this.tablesService.removeUserFromTable(this._table, user.bggNick).subscribe(data => this.refresh());
    }

    addGameToTable() {
        this.tablesService.addGameToTable(this._table, this.gameToAdd.id).subscribe(data => this.refresh());
    }

    removeGameFromTable(game: Game) {
        this.tablesService.removeGameFromTable(this._table, game.id).subscribe(data => this.refresh());
    }

    searchGame(event: string) {
        clearTimeout(this.searchTimer);
        this.searchTimer = setTimeout(
            data => {
                this.gamesService.searchGame(event).subscribe(data => this.searchResult = data);
            }, 2000
        );
    }

    selectGame(event: Game) {
        this.gameToAdd = event;
    }
    
}