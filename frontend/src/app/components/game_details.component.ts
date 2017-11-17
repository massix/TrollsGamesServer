import { Component, Input, OnInit } from '@angular/core';
import { Game } from '../data/game';
import { GamesService } from '../services/games.service';
import { HttpErrorResponse } from '@angular/common/http';
import { AlertService } from '../services/alert.service';

@Component({
    selector: 'game-details',
    template:
    `
        <div class="col-md-3 col-lg-3 col-xs-12 col-sm-12">
            <small>
                <img src="{{game.thumbnail}}"> <br />
                A game for {{game.minPlayers}} to {{game.maxPlayers}} players.<br />
                Designed by {{game.authors}}<br />
                Ranked @ {{game.rank}}<br />
                Owned by 
                <ul>
                    <li *ngFor="let owner of owners">{{owner}}</li>
                </ul>
            </small>
        </div>
        <div [innerHtml]='game.description' class="col-md-9 col-lg-9 col-xs-12 col-sm-12">
        </div>
    `
})
export class GameDetailsComponent {
    private _game: Game;
    owners: string[];

    @Input()
    set game(game: Game) {
        this._game = game;
        this.gamesService.getOwners(this._game.id).subscribe(data => this.owners = data);
    }

    get game(): Game {
        return this._game;
    }

    constructor(private gamesService: GamesService, private alertService: AlertService) {}
}