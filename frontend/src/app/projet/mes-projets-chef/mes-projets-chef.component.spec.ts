import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MesProjetsChefComponent } from './mes-projets-chef.component';

describe('MesProjetsChefComponent', () => {
  let component: MesProjetsChefComponent;
  let fixture: ComponentFixture<MesProjetsChefComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MesProjetsChefComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MesProjetsChefComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
