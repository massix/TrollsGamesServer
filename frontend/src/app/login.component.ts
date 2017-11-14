import { Component, OnInit } from "@angular/core";
import { LoginService } from "./login.service";
import { Login } from "./login";
import { HttpResponse } from "@angular/common/http";
import { User } from "./user";

@Component({
    selector: 'component',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
    login: Login = new Login();

    ngOnInit(): void {
    }

    onSubmit(): void {
        console.log("Submit button clicked");
        console.log("login: " + this.login.email);
        console.log("password: " + this.login.password);

        this.loginService.login(this.login).subscribe((data: HttpResponse<User>) => {
            console.log(data);
            console.log(data.status);

            if (data.headers.has('authorization')) {
                console.log('ok');
            }

            else if (data.headers.has('Authorization')) {
                console.log('ok upper case');
            }

            else {
                console.log('nope');
            }

            console.log(data.headers.keys().forEach(key => console.log(key)));
            console.log(data.body.bggNick);
        });
    }

    constructor(private loginService: LoginService) {

    }
}
