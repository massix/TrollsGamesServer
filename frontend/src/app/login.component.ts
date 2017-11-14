import { Component, OnInit } from '@angular/core';
import { LoginService } from './login.service';
import { Login } from './login';
import { HttpResponse } from '@angular/common/http';
import { User } from './user';
import { error } from 'util';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent {
    login: Login = new Login();
    error: string;

    onSubmit(): void {
        this.error = null;
        console.log('Submit button clicked');
        console.log('login: ' + this.login.email);
        console.log('password: ' + this.login.password);

        this.loginService.login(this.login).subscribe((data: HttpResponse<User>) => {
            if (data.headers.has('authorization')) {
                data.body.token = data.headers.get('authorization').replace('Bearer ', '');
                this.loginService.loggedUser = data.body;
                this.router.navigate(['/admin']);
            } else {
                this.error = 'Authentication failed';
            }
        },

        (err: HttpErrorResponse) => {
            this.error = err.error;

            this.login.email = '';
            this.login.password = '';
        });
    }

    constructor(private loginService: LoginService, private router: Router) {}
}
