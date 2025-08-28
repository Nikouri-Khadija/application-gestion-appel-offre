import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MesTachesComponent } from './mes-taches.component';

describe('MesTachesComponent', () => {
  let component: MesTachesComponent;
  let fixture: ComponentFixture<MesTachesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MesTachesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MesTachesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
