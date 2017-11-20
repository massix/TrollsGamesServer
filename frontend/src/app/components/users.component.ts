import { Component, OnInit } from '@angular/core';
import { LoginService } from '../services/login.service';
import { UsersService } from '../services/users.service';
import { User } from '../data/user';
import { Md5 } from 'ts-md5/dist/md5';
import { CrawlService } from '../services/crawl.service';
import { AlertService } from '../services/alert.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
    templateUrl: '../views/users.component.html',
    styleUrls: ['../styles/users.component.css']
})
export class UsersComponent implements OnInit {
    constructor(private usersService: UsersService, private crawlService: CrawlService, private alertService: AlertService) {}
    users: User[] = [];
    newUser: User = new User();

    ngOnInit() {
        this.refreshUsersList();
    }

    submitUser() {
        console.log(this.newUser);
        this.usersService.addUser(this.newUser).subscribe(data => {
            this.refreshUsersList();
        });
    }

    removeUser(user: User) {
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
            });
        });
    }

    editUser(user: User) {
        this.newUser = user;
    }

    lookupUser(user: User) {
        this.crawlService.crawlCollection(user.bggNick).subscribe(
            data => {},
            (response: HttpErrorResponse) => {
                if (response.status === 202) {
                    this.alertService.success('Crawl for user started');
                } else {
                    this.alertService.error('Error ' + response.status);
                }
            });
    }

    resetForm() {
        this.newUser = new User();
    }
}
