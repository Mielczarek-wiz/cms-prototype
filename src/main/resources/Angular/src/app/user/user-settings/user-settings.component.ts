import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {User} from 'src/assets/models/user';
import {UserService} from 'src/assets/service/user.service';

@Component({
  selector: 'app-user-settings',
  templateUrl: './user-settings.component.html',
  styleUrls: ['./user-settings.component.scss']
})
export class UserSettingsComponent implements OnInit {


  public user!: User;

  constructor(
    private route: ActivatedRoute,
    private userService: UserService) {
  }

  ngOnInit(): void {
    this.loadUser();
  }

  loadUser() {
    this.userService.getLoggedUser()
      .subscribe(res => {
        this.user = res;
      });
  }

  // startEdit() {
  //   let dialogData = {
  //     data: {
  //       edit: true,
  //       user: this.user
  //     }
  //   }
  //
  //
  //   dialogRef.afterClosed().subscribe(result => {
  //     this.loadUsers();
  //   });
  // }

}
