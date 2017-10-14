import {Injectable} from '@angular/core';
import {User} from './user';
import {HttpClient} from '@angular/common/http';
import 'rxjs/add/operator/toPromise';

@Injectable()
export class UsersService {
  apiBase = "http://massi.rocks:8180/v1";
  cors = "https://cors-anywhere.herokuapp.com/";

  users: User[];

  constructor(private httpClient: HttpClient) {}

  fetchUsers() : Promise<User[]> {
    return this.httpClient.get<User[]>(this.cors + this.apiBase +  "/users/get")
                          .toPromise();
  }
}
