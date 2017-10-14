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
  userGames: Game[] = [];

  ngOnInit() {
    this.usersService.fetchUsers().then(users => this.users = users);
  }

  onSelect(user: User) {
    this.selectedUser = user;
    let splitGames = this.selectedUser.games.split(" ");
    this.userGames = []

    for (let game of splitGames) {
      this.gamesService.getGame(+game).then(game => this.userGames.push(game));
    }
  }

  constructor(private usersService: UsersService, private gamesService: GamesService) {

  }
}
