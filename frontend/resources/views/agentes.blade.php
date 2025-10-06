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
                <a href="/agentes" class="flex items-center gap-2 py-3 px-4 rounded bg-blue-900 text-white font-semibold mb-2">
                    <i class="fa fa-user"></i>    
                    Mis Agentes
                </a>

                <a href="/citas" class="flex items-center gap-2 py-3 px-4 rounded hover:bg-gray-200 font-semibold mb-2">
                    <i class="fa fa-calendar"></i>    
                    Citas
                </a>

            </div>
            
        </div>

    </div>

    <main class="sm:ml-64 p-7">
        <h1 class="text-3xl font-bold">Agentes</h1>

        <div class="flex justify-end mb-4">
            <!-- Open the modal using ID.showModal() method -->
            <button class="btn bg-blue-900 text-white" onclick="modal_create_agente.showModal()">Crear Agente</button>
            <dialog id="modal_create_agente" class="modal">
                <div class="modal-box">
                    <h3 class="text-lg font-bold">Crea a tu agente</h3>
                    <p class="py-4">Completa el formulario para crear un nuevo agente</p>
                    <form action="{{ route('agentes.crear') }}" method="POST" class="flex flex-col gap-4">
                            @csrf
                            <input type="text" name="nombre" placeholder="Nombre" class="input w-full" />
                            <input type="email" name="email" placeholder="Email" class="input w-full" />
                            <input type="text" name="especialidad" placeholder="Especialidad" class="input w-full" />

                            <div class="flex gap-2 justify-end mt-4">
                                <button type="submit" class="btn bg-blue-900 text-white">Crear</button>
                                <button type="button" class="btn" onclick="modal_create_agente.close()">Close</button>
                            </div>
                        </form>
    
                    
                </div>
            </dialog>

        </div>

        <div class="mt-5 overflow-x-auto rounded-box border border-base-content/5 bg-base-100">
            <table class="table">
                <!-- head -->
                <thead>
                <tr>
                    <th></th>
                    <th>Nombre</th>
                    <th>Email</th>
                    <th>Especialidad</th>
                    <th>Acciones</th>
                </tr>
                </thead>
                <tbody>
                    @foreach ($agentes as $index => $agente)
                    <tr>
                        <th>{{ $index + 1 }}</th>
                        <td>{{ $agente['nombre'] }}</td>
                        <td>{{ $agente['email'] }}</td>
                        <td>{{ $agente['especialidad'] }}</td>
                        <td>

                            <!-- Botón Ver -->
                        <button class="btn btn-sm" onclick="document.getElementById('modal_ver_{{ $agente['id'] }}').showModal()">Ver</button>

                        <!-- Modal Ver Dinámico -->
                        <dialog id="modal_ver_{{ $agente['id'] }}" class="modal">
                            <div class="modal-box">
                                <h3 class="text-lg font-bold">Información del agente</h3>
                                <p class="py-4">Revisa la información de tu agente</p>
                                    
                                <div class="flex flex-col gap-2">
                                    <p><strong>ID:</strong> {{ $agente['id'] }}</p>
                                    <p><strong>Nombre:</strong> {{ $agente['nombre'] }}</p>
                                    <p><strong>Email:</strong> {{ $agente['email'] }}</p>   
                                    <p><strong>Especialidad:</strong> {{ $agente['especialidad'] }}</p>
                                    <p><strong>Activo:</strong> 
                                        @if($agente['activo'])
                                            <span class="text-green-600 font-semibold">Sí</span>
                                        @else
                                            <span class="text-red-600 font-semibold">No</span>
                                        @endif
                                    </p>
                                </div>

                                <div class="flex gap-2 justify-end mt-4">
                                    <button type="button" class="btn" onclick="document.getElementById('modal_ver_{{ $agente['id'] }}').close()">Cerrar</button>
                                </div>
                            </div>
                        </dialog>



                            <!-- Botón Editar -->
                            <button class="btn btn-sm" onclick="document.getElementById('modal_edit_{{ $agente['id'] }}').showModal()">Editar</button>

                            <!-- Modal de edición -->
                            <dialog id="modal_edit_{{ $agente['id'] }}" class="modal">
                                <div class="modal-box">
                                    <h3 class="text-lg font-bold">Editar Agente</h3>
                                    <p class="py-4">Modifica la información de tu agente</p>
                                    <form method="POST" action="{{ route('agentes.actualizar', $agente['id']) }}">
                                    @csrf
                                    @method('PUT')

                                    <div class="flex flex-col gap-4">
                                        <input type="text" name="nombre" class="input w-full" value="{{ $agente['nombre'] }}" />
                                    <input type="email" name="email" class="input w-full" value="{{ $agente['email'] }}" />
                                    <input type="text" name="especialidad" class="input w-full" value="{{ $agente['especialidad'] }}" />
                                    </div>

                                    <div class="flex gap-2 justify-end mt-4">
                                        <button type="submit" class="btn bg-blue-900 text-white">Actualizar</button>
                                        <button type="button" class="btn" onclick="document.getElementById('modal_edit_{{ $agente['id'] }}').close()">Close</button>


                                    </div>

                                </form>

                                </div>
                            </dialog>

                            <!-- Botón Eliminar -->
                            <button class="btn btn-sm btn-error" onclick="document.getElementById('modal_delete_{{ $agente['id'] }}').showModal()">Eliminar</button>

                            <!-- Modal de confirmación -->
                            <dialog id="modal_delete_{{ $agente['id'] }}" class="modal">
                                <div class="modal-box">
                                    <h3 class="text-lg font-bold">Eliminar Agente</h3>
                                    <p class="py-4">¿Estás seguro de que deseas eliminar a <strong>{{ $agente['nombre'] }}</strong>?</p>
                                    <form method="POST" action="{{ route('agentes.eliminar', $agente['id']) }}">
                                        @csrf
                                        @method('DELETE')
                                        <div class="flex gap-2 justify-end mt-4">
                                            <button type="submit" class="btn btn-error">Eliminar</button>
                                            <button type="button" class="btn" onclick="document.getElementById('modal_delete_{{ $agente['id'] }}').close()">Cancelar</button>
                                        </div>
                                    </form>
                                </div>
                            </dialog>

                        </td>
                    </tr>
                    @endforeach
                </tbody>

            </table>
        </div>

        

    </main>
    
</body>
</html>
