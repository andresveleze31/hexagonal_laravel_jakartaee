<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use GuzzleHttp\Client;

class AgentesController extends Controller
{
    //
    public function getAgents() {
        $agentes = [];
        $total = 0;

        try {
            $client = new Client([
                'base_uri' => 'http://localhost:9080', // tu backend Jakarta EE
                'timeout'  => 5.0,
            ]);

            $response = $client->get('/rest/agentes');
            $body = json_decode($response->getBody(), true);

            $agentes = $body['agentes'] ?? [];
            $total = $body['total'] ?? 0;

        } catch (\Exception $e) {
            // En caso de error, puedes loguear o mostrar mensaje
            \Log::error('Error al obtener agentes: ' . $e->getMessage());
        }

        return view('agentes', compact('agentes', 'total'));
    }

    public function crearAgente(Request $request)
    {
        // Validación básica en Laravel
        $data = $request->validate([
            'nombre' => 'required|string',
            'email' => 'required|email',
            'especialidad' => 'nullable|string',
        ]);

        try {
            $client = new Client([
                'base_uri' => 'http://localhost:9080', // URL de tu backend Jakarta EE
            ]);

            $response = $client->post('/rest/agentes', [
                'json' => $data
            ]);

            $body = json_decode($response->getBody(), true);

            // Redirigir con mensaje de éxito
            return redirect()->back()->with('success', $body['mensaje'] ?? 'Agente creado correctamente');

        } catch (\GuzzleHttp\Exception\RequestException $e) {
            $error = $e->hasResponse() ? $e->getResponse()->getBody()->getContents() : $e->getMessage();
            return redirect()->back()->with('error', 'Error creando agente: ' . $error);
        }
    }

    public function eliminarAgente($id)
    {
        try {
            $client = new Client(['base_uri' => 'http://localhost:9080']);
            $response = $client->delete("/rest/agentes/{$id}");

            $body = json_decode($response->getBody(), true);

            return redirect()->route('agentes.index')
                            ->with('success', $body['mensaje'] ?? 'Agente eliminado correctamente');
        } catch (\Exception $e) {
            return redirect()->route('agentes.index')
                            ->with('error', 'Error al eliminar agente: ' . $e->getMessage());
        }
    }

    public function actualizarAgente(Request $request, $id)
    {
        // Validación de datos del formulario
        $data = $request->validate([
            'nombre' => 'required|string',
            'email' => 'required|email',
            'especialidad' => 'nullable|string',
        ]);

        try {
            // 🔹 Construir el JSON exactamente como tu backend lo espera
            $jsonData = [
                'id' => (int) $id,
                'nombre' => $data['nombre'],
                'especialidad' => $data['especialidad'] ?? '',
                'email' => $data['email'],
                'activo' => true,   // fijo en true
                'citas' => []       // vacío por ahora
            ];

            $client = new \GuzzleHttp\Client([
                'base_uri' => 'http://localhost:9080',
                'timeout' => 5.0,
            ]);

            // 🔹 Realizar petición PUT con JSON
            $response = $client->put("/rest/agentes/{$id}", [
                'json' => $jsonData
            ]);

            $body = json_decode($response->getBody(), true);

            return redirect()->back()
                ->with('success', $body['mensaje'] ?? 'Agente actualizado correctamente');

        } catch (\GuzzleHttp\Exception\RequestException $e) {
            $error = $e->hasResponse()
                ? $e->getResponse()->getBody()->getContents()
                : $e->getMessage();

            return redirect()->back()
                ->with('error', 'Error al actualizar agente: ' . $error);
        } catch (\Exception $e) {
            return redirect()->back()
                ->with('error', 'Error inesperado: ' . $e->getMessage());
        }
    }




}
