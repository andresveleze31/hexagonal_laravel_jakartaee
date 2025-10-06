<!-- resources/views/agentes/index.blade.php -->
<!DOCTYPE html>
<html>
<head>
    <title>Lista de Agentes</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Outfit:wght@100..900&display=swap" rel="stylesheet">
    @vite('resources/css/app.css')
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" integrity="sha512-..." crossorigin="anonymous" referrerpolicy="no-referrer" />

</head>
<body class="bg-gray-100 h-screen">

    <div class="sm:w-64 hidden sm:block fixed">
        <div class="p-5 bg-white shadow-sm h-screen">
            <div class="flex gap-2 items-center pb-7 border-b border-b-gray-300">
                <img src="{{ asset('/logo.svg') }}" alt="Logo">
                <span class="font-bold text-2xl">HexManage</span>
            </div>

            <div class="mt-7 flex flex-col gap-2">
                <a href="/agentes" class="flex items-center gap-2 py-3 px-4 rounded hover:bg-gray-200 font-semibold mb-2">
                    <i class="fa fa-user"></i>    
                    Mis Agentes
                </a>

                <a href="/citas" class="flex items-center gap-2 py-3 px-4 rounded bg-blue-900 text-white font-semibold mb-2">
                    <i class="fa fa-calendar"></i>    
                    Citas
                </a>

            </div>
            
        </div>

    </div>

    <main class="sm:ml-64 p-7">
        <h1 class="text-3xl font-bold">Citas</h1>

        <div class="flex justify-end mb-4">
            <!-- Open the modal using ID.showModal() method -->
             <button class="btn bg-blue-900 text-white" onclick="modal_create_cita.showModal()">Agendar Cita</button>

            <dialog id="modal_create_cita" class="modal">
                <div class="modal-box">
                    <h3 class="text-lg font-bold mb-4">Agenda tu cita</h3>

                    <form method="POST" action="{{ route('citas.crear') }}">
                        @csrf

                        <!-- Seleccionar agente -->
                        <div class="mb-4">
                            <label class="block font-semibold mb-1">Seleccionar Agente</label>
                            <select name="agenteId" class="select select-bordered w-full" required>
                                <option value="">-- Selecciona un agente --</option>
                                @foreach($agentes as $agente)
                                    <option value="{{ $agente['id'] }}">{{ $agente['nombre'] }}</option>
                                @endforeach
                            </select>
                        </div>

                        <!-- Nombre del cliente -->
                        <div class="mb-4">
                            <label class="block font-semibold mb-1">Nombre del Cliente</label>
                            <input type="text" name="clienteNombre" class="input input-bordered w-full" required>
                        </div>

                        <!-- Correo del cliente -->
                        <div class="mb-4">
                            <label class="block font-semibold mb-1">Email del Cliente</label>
                            <input type="email" name="clienteEmail" class="input input-bordered w-full" required>
                        </div>

                        <!-- Fecha y hora -->
                        <div class="mb-4">
                            <label class="block font-semibold mb-1">Fecha y Hora</label>
                            <input type="datetime-local" name="fechaHora" class="input input-bordered w-full" required>
                        </div>

                        <!-- Motivo -->
                        <div class="mb-4">
                            <label class="block font-semibold mb-1">Motivo</label>
                            <textarea name="motivo" class="textarea textarea-bordered w-full" required></textarea>
                        </div>

                        <!-- Botones -->
                        <div class="flex justify-end gap-2 mt-4">
                            <button type="button" class="btn" onclick="modal_create_cita.close()">Cancelar</button>
                            <button type="submit" class="btn bg-blue-900 text-white">Guardar</button>
                        </div>
                    </form>
                </div>
            </dialog>

        </div>

       <div class="mt-5 overflow-x-auto rounded-box border border-base-content/5 bg-base-100">
            <table class="table">
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Nombre Cliente</th>
                        <th>Agente</th>
                        <th>Fecha</th>
                        <th>Motivo</th>
                        <th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    @forelse($citas as $index => $cita)
                        <tr>
                            <th>{{ $index + 1 }}</th>
                            <td>{{ $cita['clienteNombre'] ?? '—' }}</td>
                            <td>
                                @php
                                    $agente = collect($agentes)->firstWhere('id', $cita['agenteId'] ?? null);
                                @endphp
                                {{ $agente['nombre'] ?? 'Sin asignar' }}
                            </td>
                            <td>{{ $cita['fechaHora'] ?? '—' }}</td>
                            <td>{{ $cita['motivo'] ?? '—' }}</td>
                            <td class="flex gap-2">
                                <!-- Botón Ver -->
                                <!-- Botón Ver Detalle de Cita -->
                                <button class="btn btn-sm" onclick="document.getElementById('modal_ver_cita_{{ $cita['id'] }}').showModal()">Ver</button>

                                <!-- Modal Detalle Cita -->
                                <dialog id="modal_ver_cita_{{ $cita['id'] }}" class="modal">
                                    <div class="modal-box">
                                        <h3 class="text-lg font-bold mb-3">Detalle de la Cita</h3>

                                        <div class="flex flex-col gap-2">
                                            <p><strong>ID Cita:</strong> {{ $cita['id'] }}</p>
                                            <p><strong>Cliente:</strong> {{ $cita['clienteNombre'] }}</p>
                                            <p><strong>Email:</strong> {{ $cita['clienteEmail'] }}</p>
                                            <p><strong>Agente:</strong> {{ $agente['nombre'] ?? 'Sin asignar' }}</p>
                                            <p><strong>Fecha y Hora:</strong> {{ $cita['fechaHora'] }}</p>
                                            <p><strong>Motivo:</strong> {{ $cita['motivo'] }}</p>
                                        </div>

                                        <div class="flex gap-2 justify-end mt-4">
                                            <button type="button" class="btn" onclick="document.getElementById('modal_ver_cita_{{ $cita['id'] }}').close()">Cerrar</button>
                                        </div>
                                    </div>
                                </dialog>

                                <!-- Botón Editar -->
                            <!-- Botón Editar -->
                            <button class="btn btn-sm" onclick="document.getElementById('modal_edit_cita_{{ $cita['id'] }}').showModal()">Editar</button>

                            <!-- Modal de edición de cita -->
                            <dialog id="modal_edit_cita_{{ $cita['id'] }}" class="modal">
                                <div class="modal-box">
                                    <h3 class="text-lg font-bold mb-3">Editar Cita</h3>
                                    <p class="text-sm text-gray-500 mb-4">Modifica la información de la cita seleccionada.</p>

                                    <form method="POST" action="{{ route('citas.actualizar', $cita['id']) }}">
                                        @csrf
                                        @method('PUT')

                                        <!-- Seleccionar agente -->
                                        <div class="mb-4">
                                            <label class="block font-semibold mb-1">Seleccionar Agente</label>
                                            <select name="agenteId" class="select select-bordered w-full" required>
                                                <option value="">-- Selecciona un agente --</option>
                                                @foreach($agentes as $agente)
                                                    <option value="{{ $agente['id'] }}" 
                                                        {{ $cita['agenteId'] == $agente['id'] ? 'selected' : '' }}>
                                                        {{ $agente['nombre'] }}
                                                    </option>
                                                @endforeach
                                            </select>
                                        </div>

                                        <!-- Nombre del cliente -->
                                        <div class="mb-4">
                                            <label class="block font-semibold mb-1">Nombre del Cliente</label>
                                            <input type="text" name="clienteNombre" class="input input-bordered w-full"
                                                value="{{ $cita['clienteNombre'] }}" required>
                                        </div>

                                        <!-- Correo del cliente -->
                                        <div class="mb-4">
                                            <label class="block font-semibold mb-1">Email del Cliente</label>
                                            <input type="email" name="clienteEmail" class="input input-bordered w-full"
                                                value="{{ $cita['clienteEmail'] }}" required>
                                        </div>

                                        <!-- Fecha y hora -->
                                        <div class="mb-4">
                                            <label class="block font-semibold mb-1">Fecha y Hora</label>
                                            <input type="datetime-local" name="fechaHora" class="input input-bordered w-full"
                                                value="{{ \Carbon\Carbon::parse($cita['fechaHora'])->format('Y-m-d\TH:i') }}" required>
                                        </div>

                                        <!-- Motivo -->
                                        <div class="mb-4">
                                            <label class="block font-semibold mb-1">Motivo</label>
                                            <textarea name="motivo" class="textarea textarea-bordered w-full" required>{{ $cita['motivo'] }}</textarea>
                                        </div>

                                        <!-- Botones -->
                                        <div class="flex justify-end gap-2 mt-5">
                                            <button type="button" class="btn" onclick="document.getElementById('modal_edit_cita_{{ $cita['id'] }}').close()">Cancelar</button>
                                            <button type="submit" class="btn bg-blue-900 text-white">Actualizar</button>
                                        </div>
                                    </form>
                                </div>
                            </dialog>

                            
                            
                            <!-- Botón Eliminar Cita -->
                            <button class="btn btn-sm btn-error" onclick="document.getElementById('modal_delete_{{ $cita['id'] }}').showModal()">Eliminar</button>

                            <!-- Modal de confirmación de eliminación de cita -->
                            <dialog id="modal_delete_{{ $cita['id'] }}" class="modal">
                                <div class="modal-box">
                                    <h3 class="text-lg font-bold">Eliminar Cita</h3>
                                    <p class="py-4">¿Estás seguro de que deseas eliminar la cita de <strong>{{ $cita['clienteNombre'] }}</strong>?</p>
                                    <form method="POST" action="{{ route('citas.eliminar', $cita['id']) }}">
                                        @csrf
                                        @method('DELETE')
                                        <div class="flex gap-2 justify-end mt-4">
                                            <button type="submit" class="btn btn-error">Eliminar</button>
                                            <button type="button" class="btn" onclick="document.getElementById('modal_delete_{{ $cita['id'] }}').close()">Cancelar</button>
                                        </div>
                                    </form>
                                </div>
                            </dialog>


                            </td>
                        </tr>
                    @empty
                        <tr>
                            <td colspan="6" class="text-center py-4 text-gray-500">
                                No hay citas registradas.
                            </td>
                        </tr>
                    @endforelse
                </tbody>
            </table>
        </div>


        

    </main>
    
</body>
</html>
