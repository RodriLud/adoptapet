import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListaMascotas } from './lista-mascotas';

describe('ListaMascotas', () => {
  let component: ListaMascotas;
  let fixture: ComponentFixture<ListaMascotas>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListaMascotas],
    }).compileComponents();

    fixture = TestBed.createComponent(ListaMascotas);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
