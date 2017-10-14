import {Component, OnInit} from '@angular/core';
import {UsersService} from './users.service';
import {GamesService} from './games.service';

import {User} from './user';
import {Game} from './game';

@Component({
  selector: 'users',
  templateUrl: './users.component.html'
})
export class UsersComponent implements OnInit {
  users: User[];
  selectedUser: User;
  selectedGame: Game;
  userGames: Game[] = [];

  ngOnInit() {
    this.usersService.fetchUsers().then(users => this.users = users);
  }

  onSelect(user: User) {
    this.selectedUser = user;
    let splitGames = this.selectedUser.games.split(" ");
    this.userGames = []

    this.gamesService.getCollection(this.selectedUser.bggNick).then(collection => this.userGames = collection);
  }

  selectGame(game: Game) {
    this.selectedGame = game;
  }

  constructor(private usersService: UsersService, private gamesService: GamesService) {}


}
