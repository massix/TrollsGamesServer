import { Component, OnInit } from '@angular/core';
import { LoginService } from './login.service';
import { UsersService } from './users.service';
import { User } from './user';
import { Md5 } from 'ts-md5/dist/md5';

@Component({
    templateUrl: './users.component.html',
    styleUrls: ['./users.component.css']
})
export class UsersComponent implements OnInit {
    constructor(private usersService: UsersService) {}
    users: User[] = [];
    newUser: User = new User();

    ngOnInit() {
        this.refreshUsersList();
    }

    submitUser() {
        console.log('submitting user');
        console.log(this.newUser);
        this.usersService.addUser(this.newUser).subscribe(data => {
            this.refreshUsersList();
        });
    }

    removeUser(user: User) {
        console.log('removing user');
        console.log(user);

        this.usersService.removeUser(user).subscribe(data => {
            this.refreshUsersList();
        });
    }

    refreshUsersList() {
        this.users = [];
        this.usersService.getAllUsers().subscribe(data => {
            this.users = data;
            data.forEach(user => {
                user.gravatarHash = Md5.hashStr(user.email);
                console.log(user);
            });
        });
    }

    editUser(user: User) {
        console.log('editing');
        this.newUser = user;
        console.log(user);
    }

    lookupUser(user: User) {
        console.log('display games for user');
        console.log(user);
    }

    resetForm() {
        this.newUser = new User();
    }
}
