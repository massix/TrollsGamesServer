import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { NgxPaginationModule } from 'ngx-pagination';

import { AppComponent } from './components/app.component';

import { LoginService } from './services/login.service';
import { LoginComponent } from './components/login.component';
import { GamesComponent } from './components/games.component';
import { AdminComponent } from './components/admin.component';
import { User } from './data/user';
import { UsersComponent } from './components/users.component';
import { AlertService } from './services/alert.service';
import { AlertComponent } from './components/alert.component';
import { LogoutComponent } from './components/logout.component';
import { UsersService } from './services/users.service';
import { AuthGuard } from './auth.guard';
import { GamesService } from './services/games.service';
import { GameDetailsComponent } from './components/game_details.component';
import { CrawlerComponent } from './components/crawler.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    AdminComponent,
    UsersComponent,
    AlertComponent,
    LogoutComponent,
    GamesComponent,
    CrawlerComponent,
    GameDetailsComponent
  ],
  imports: [
    NgxPaginationModule,
    BrowserModule,
    FormsModule,
    HttpClientModule,
    RouterModule.forRoot([
      {
        path: 'login',
        component: LoginComponent
      },
      {
        path: 'logout',
        component: LogoutComponent
      },
      {
        path: 'admin',
        component: AdminComponent,
        canActivate: [AuthGuard],
        children: [
          {
            path: 'users',
            component: UsersComponent,
            outlet: 'adminoutlet',
            canActivate: [AuthGuard]
          },
          {
            path: 'games',
            component: GamesComponent,
            outlet: 'adminoutlet',
            canActivate: [AuthGuard]
          },
          {
            path: 'crawler',
            component: CrawlerComponent,
            outlet: 'adminoutlet',
            canActivate: [AuthGuard]
          }
        ]
      },
      {
        path: '',
        redirectTo: '/login',
        pathMatch: 'full'
      }
    ])
  ],
  providers: [LoginService, AlertService, UsersService, GamesService ,AuthGuard],
  bootstrap: [AppComponent]
})
export class AppModule { }
