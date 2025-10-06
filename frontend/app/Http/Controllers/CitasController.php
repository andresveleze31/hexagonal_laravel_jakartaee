<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use GuzzleHttp\Client;

class CitasController extends Controller
{
    
     // 🔹 Mostrar vista con lista de agentes
    public function getCitas()
    {
        $agentes = [];
        $citas = [];

        try {
            $client = new Client([
                'base_uri' => 'http://localhost:9080',
                'timeout'  => 5.0,
            ]);

            // 🔸 Obtener agentes
            $responseAgentes = $client->get('/rest/agentes');
            $bodyAgentes = json_decode($responseAgentes->getBody(), true);
            $agentes = $bodyAgentes['agentes'] ?? [];

            // 🔸 Obtener citas
            $responseCitas = $client->get('/rest/citas');
            $bodyCitas = json_decode($responseCitas->getBody(), true);
            $citas = $bodyCitas['citas'] ?? $bodyCitas ?? []; // soporte para ambos formatos

        } catch (\Exception $e) {
            \Log::error('Error al obtener datos: ' . $e->getMessage());
        }

        // 🔸 Pasamos ambos arrays a la vista
        return view('citas', compact('agentes', 'citas'));
    }

    // 🔹 Crear cita y enviarla a Jakarta EE
    public function crearCita(Request $request)
    {
        $data = $request->validate([
            'agenteId' => 'required|integer',
            'clienteNombre' => 'required|string',
            'clienteEmail' => 'required|email',
            'fechaHora' => 'required|date',
            'motivo' => 'required|string',
        ]);

        try {
            $client = new Client([
                'base_uri' => 'http://localhost:9080',
                'timeout' => 5.0,
            ]);

            $response = $client->post('/rest/citas', [
                'json' => $data,
            ]);

            $body = json_decode($response->getBody(), true);

            return redirect()->back()->with('success', $body['mensaje'] ?? 'Cita creada correctamente');

        } catch (\GuzzleHttp\Exception\RequestException $e) {
            $error = $e->hasResponse() ? $e->getResponse()->getBody()->getContents() : $e->getMessage();
            return redirect()->back()->with('error', 'Error al crear cita: ' . $error);
        }
    }



    public function actualizarCita(Request $request, $id)
    {
        // ✅ Validar los datos del formulario
        $data = $request->validate([
            'agenteId' => 'required|integer',
            'clienteNombre' => 'required|string',
            'clienteEmail' => 'required|email',
            'fechaHora' => 'required|date',
            'motivo' => 'required|string',
        ]);

        try {
            // 🔹 Construir el JSON que tu backend (Jakarta EE) espera
            $jsonData = [
                'id' => (int) $id,
                'agenteId' => (int) $data['agenteId'],
                'clienteNombre' => $data['clienteNombre'],
                'clienteEmail' => $data['clienteEmail'],
                'fechaHora' => $data['fechaHora'],
                'motivo' => $data['motivo']
            ];

            // 🔹 Configurar el cliente HTTP
            $client = new Client([
                'base_uri' => 'http://localhost:9080',
                'timeout' => 5.0,
            ]);

            // 🔹 Enviar la petición PUT al backend
            $response = $client->put("/rest/citas/{$id}", [
                'json' => $jsonData
            ]);

            $body = json_decode($response->getBody(), true);

            return redirect()->back()
                ->with('success', $body['mensaje'] ?? 'Cita actualizada correctamente');

        } catch (\GuzzleHttp\Exception\RequestException $e) {
            $error = $e->hasResponse()
                ? $e->getResponse()->getBody()->getContents()
                : $e->getMessage();

            return redirect()->back()
                ->with('error', 'Error al actualizar la cita: ' . $error);

        } catch (\Exception $e) {
            return redirect()->back()
                ->with('error', 'Error inesperado: ' . $e->getMessage());
        }
    }


   // Eliminar cita
public function eliminarCita($id)
{
    try {
        $client = new Client([
            'base_uri' => 'http://localhost:9080',
            'timeout' => 5.0,
        ]);

        $response = $client->delete("/rest/citas/{$id}");
        $body = json_decode($response->getBody(), true);

        // ✅ Aquí usamos el nombre correcto de la ruta
        return redirect()->route('citas.index')
            ->with('success', $body['mensaje'] ?? 'Cita eliminada correctamente');

    } catch (\Exception $e) {
        return redirect()->route('citas.index')
            ->with('error', 'Error al eliminar cita: ' . $e->getMessage());
    }
}


}
