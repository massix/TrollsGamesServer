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

  ngOnInit() {
    this.usersService.fetchUsers().then(users => {
      this.users = users;
      this.users.forEach(user => this.gamesService.getCollection(user.bggNick).then(collection => user.collection = collection));
    });
  }

  onSelect(user: User) {
    if (this.selectedUser)
      this.selectedUser.active = !this.selectedUser.active;

    this.selectedUser = user;
    let splitGames = this.selectedUser.games.split(" ");

    user.active = !user.active;
  }

  constructor(private usersService: UsersService, private gamesService: GamesService) {}
}
