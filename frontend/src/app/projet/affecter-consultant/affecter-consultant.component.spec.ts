import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AffecterConsultantComponent } from './affecter-consultant.component';

describe('AffecterConsultantComponent', () => {
  let component: AffecterConsultantComponent;
  let fixture: ComponentFixture<AffecterConsultantComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AffecterConsultantComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AffecterConsultantComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
