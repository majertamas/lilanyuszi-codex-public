import {Component, inject, OnInit} from '@angular/core';
import {ShoppingListService} from './shopping-list.service';
import {Group, GroupAliasRequest, GroupMemberResponse, GroupRole} from '../../model/group.model';
import {InputText} from 'primeng/inputtext';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {TableModule} from 'primeng/table';
import {Button, ButtonDirective} from 'primeng/button';
import {Ripple} from 'primeng/ripple';
import {Tag} from 'primeng/tag';
import {Card} from 'primeng/card';
import {UserService} from '../../service/user-service';
import {GroupAliasService} from '../../service/group-alias.service';
import {MessageService} from 'primeng/api';

@Component({
  selector: 'app-shopping-lists',
  imports: [
    InputText,
    ReactiveFormsModule,
    TableModule,
    FormsModule,
    ButtonDirective,
    Ripple,
    Tag,
    Card,
    Button
  ],
  templateUrl: './shopping-lists.html',
  styleUrl: './shopping-lists.css',
})
export class ShoppingLists implements OnInit {

  private readonly shoppingListService = inject(ShoppingListService);
  private readonly userService = inject(UserService);
  private readonly groupAliasService = inject(GroupAliasService);
  private readonly messageService = inject(MessageService);
  protected shoppingLists: Group[] = [];
  private clonedShoppingLists: { [s: string]: Group } = {};
  protected renaming: boolean = false;
  protected newName: string = '';

  ngOnInit(): void {
    this.getLists();
  }

  private getLists() {
    this.shoppingListService.getShoppingListsWhereMember().subscribe({
      next: (groups: Group[]): void => {
        this.shoppingLists = groups;
      }
    });
  }

  onRowEditInit(shoppingList: Group) {
    this.clonedShoppingLists[shoppingList.id] = {...shoppingList};
  }

  onRowEditSave(shoppingList: Group, index: number) {
    this.shoppingListService.modifyGroupName({id: shoppingList.id, newName: shoppingList.name})
      .subscribe({
        next: (group: Group): void => {
          if (group) {
            this.shoppingLists[index] = group;
            delete this.clonedShoppingLists[shoppingList.id];
          } else {
            this.onRowEditCancel(shoppingList, index);
          }
        }
      });
  }

  onRowEditCancel(shoppingList: Group, index: number) {
    this.shoppingLists[index] = this.clonedShoppingLists[shoppingList.id];
    delete this.clonedShoppingLists[shoppingList.id];
  }

  getSeverity(role: GroupRole) {
    switch (role) {
      case 'OWNER':
        return 'success';
      case 'ADMIN':
        return 'success';
      case 'MEMBER':
        return 'info';
    }
  }

  protected getOwner(shoppingList: Group): GroupMemberResponse | undefined {
    return shoppingList.members.find(m => m.role === 'OWNER');
  }

  protected getMembers(shoppingList: Group): GroupMemberResponse[] {
    const members = shoppingList.members
      .filter(m => m.role !== 'OWNER')
    return [...members].sort((a, b) => a.name.localeCompare(b.name));
  }

  protected deleteOrLeave(shoppingList: Group) {
    if (shoppingList.isOwner) {
      this.shoppingListService.deleteShoppingList(shoppingList.id).subscribe(
        {
          next: (): void => {
            this.getLists();
          }
        }
      );
    } else {
      this.shoppingListService.leaveShoppingList(shoppingList.id).subscribe(
        {
          next: (): void => {
            this.getLists();
          }
        }
      );
    }

  }

  protected removeMember(shoppingList: Group, member: GroupMemberResponse) {
    this.shoppingListService.removeMember(shoppingList, member).subscribe(
      {
        next: (response: Group[] | null): void => {
          if (response) {
            this.shoppingLists = response;
          }
        }
      }
    );
  }

  protected isOwner(group: Group) {
    return this.userService.user()?.id === this.getOwner(group)?.userId;
  }

  protected rename() {
    this.renaming = true;
  }

  protected doRename(shoppingList: Group) {
    if (this.newName.trim() === '') {
      this.messageService.add({severity: 'error', summary: 'Error', detail: 'Name cannot be empty'});
      return;
    }
    const groupId: number = shoppingList.id;
    const request: GroupAliasRequest = {
      groupId: groupId,
      alias: this.newName
    }

    this.groupAliasService.saveAlias(request).subscribe({
      next: (): void => {
        this.getLists();
        this.renaming = false;
        this.newName = '';
      }
    });
  }
}
