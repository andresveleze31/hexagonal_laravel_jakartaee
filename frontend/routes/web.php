<?php

use App\Http\Controllers\ProfileController;
use App\Http\Controllers\AgentesController;
use App\Http\Controllers\CitasController;
use Illuminate\Support\Facades\Route;


Route::get('/', function () {
    return view('welcome');
});

Route::get('/dashboard', function () {
    return view('dashboard');
})->middleware(['auth', 'verified'])->name('dashboard');

Route::middleware('auth')->group(function () {
    Route::get('/profile', [ProfileController::class, 'edit'])->name('profile.edit');
    Route::patch('/profile', [ProfileController::class, 'update'])->name('profile.update');
    Route::delete('/profile', [ProfileController::class, 'destroy'])->name('profile.destroy');
});

Route::middleware('auth')->group(function () {
    Route::get('/agentes', [AgentesController::class, 'getAgents'])->name('agentes');
    Route::post('/agentes/crear', [AgentesController::class, 'crearAgente'])->name('agentes.crear');
    Route::put('/agentes/{id}', [AgentesController::class, 'actualizarAgente'])->name('agentes.actualizar');
    Route::delete('/agentes/{id}', [AgentesController::class, 'eliminarAgente'])->name('agentes.eliminar');

    Route::get('/citas', [CitasController::class, 'getCitas'])->name('citas.index');;
    Route::post('/citas', [CitasController::class, 'crearCita'])->name('citas.crear');

    Route::put('/citas/{id}', [CitasController::class, 'actualizarCita'])->name('citas.actualizar');
    Route::delete('/citas/{id}', [CitasController::class, 'eliminarCita'])->name('citas.eliminar');


});

require __DIR__.'/auth.php';
