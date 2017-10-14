import {Component, OnInit} from '@angular/core';
import { GamesService } from './games.service';
import {Game} from './game';

@Component({
  selector: 'games',
  templateUrl: './games.component.html'
})
export class GamesComponent implements OnInit {
  games: Game[];
  selectedGame: Game;

  ngOnInit() {
    this.gamesService.getGames().then(games => this.games = games);
  }

  constructor(private gamesService: GamesService) {}

  onSelect(game: Game)  {
    this.selectedGame = game
  }
}
