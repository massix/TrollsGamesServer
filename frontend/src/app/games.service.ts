import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Game} from './game';
import 'rxjs/add/operator/toPromise';

@Injectable()
export class GamesService {
  apiBase = "http://staging.massi.rocks/v1";

  constructor(private httpClient : HttpClient) {}

  getCollection(user: string) : Promise<Game[]> {
    return this.httpClient.get<Game[]>(this.apiBase + "/collection/get/" + user).toPromise();
  }

  getGame(id: number) : Promise<Game> {
    return this.httpClient.get<Game>(this.apiBase + "/games/get/" + id).toPromise();
  }

  getGames() : Promise<Game[]> {
    return this.httpClient.get<Game[]>(this.apiBase + "/games/get").toPromise();
  }
}
