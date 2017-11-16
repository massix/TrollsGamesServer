import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';

import { LoginService } from './login.service';
import { LoginComponent } from './login.component';
import { GamesComponent } from './games.component';
import { AdminComponent } from './admin.component';
import { User } from './user';
import { UsersComponent } from './users.component';
import { AlertService } from './alert.service';
import { AlertComponent } from './alert.component';
import { LogoutComponent } from './logout.component';
import { UsersService } from './users.service';
import { AuthGuard } from './auth.guard';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    AdminComponent,
    UsersComponent,
    AlertComponent,
    LogoutComponent,
    GamesComponent
  ],
  imports: [
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
  providers: [LoginService, AlertService, UsersService, AuthGuard],
  bootstrap: [AppComponent]
})
export class AppModule { }
