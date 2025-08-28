import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChefSelectionComponent } from './chef-selection.component';

describe('ChefSelectionComponent', () => {
  let component: ChefSelectionComponent;
  let fixture: ComponentFixture<ChefSelectionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChefSelectionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ChefSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
