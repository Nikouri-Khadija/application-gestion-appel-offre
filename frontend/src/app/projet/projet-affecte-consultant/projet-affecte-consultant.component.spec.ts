import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjetAffecteConsultantComponent } from './projet-affecte-consultant.component';

describe('ProjetAffecteConsultanComponent', () => {
  let component: ProjetAffecteConsultantComponent;
  let fixture: ComponentFixture<ProjetAffecteConsultantComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjetAffecteConsultantComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProjetAffecteConsultantComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
